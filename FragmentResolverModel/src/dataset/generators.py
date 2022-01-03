import os
import json
import generated.fragment_resolver_pb2 as fragment_resolver


class AudioDataJsonToProtoGenerator:
    def __init__(self, data_path):
        self.data_path = data_path

    def generate(self):
        for filepath in filter(
                os.path.isfile,
                map(
                    lambda filename: os.path.join(self.data_path, filename),
                    os.listdir(self.data_path)
                )
        ):
            with open(filepath, encoding='utf-8') as f:
                json_info = json.load(f)
            audio_filepath = json_info['srcFilepath']

            proto_fragments_collection = fragment_resolver.FragmentResolverModelResponse()

            for json_fragment in json_info['fragments']:
                proto_fragment = fragment_resolver.ResolvedFragment()
                proto_fragment.startUs = int(
                    json_fragment['mutableAreaStartUs']
                )
                proto_fragment.endUs = int(
                    json_fragment['mutableAreaEndUs']
                )

                proto_fragment.transformer.type = fragment_resolver.ResolvedTransformer.Type.Value(
                    json_fragment["transformer"]["type"]
                )

                if proto_fragment.transformer.type == fragment_resolver.ResolvedTransformer.Type.SILENCE:
                    proto_fragment.transformer.silenceDurationUs = \
                        json_fragment["transformer"]["durationUs"]
                else:
                    raise ValueError('Unsupported transformer type')

                #### REMOVE LATER ####
                # import random
                # random_type = random.randint(0, 2)
                # if random_type == audio_process_pipeline_pb2.Transformer.TransformerType.SILENCE:
                #     proto_fragment.transformer.type = audio_process_pipeline_pb2.Transformer.TransformerType.SILENCE
                #     proto_fragment.transformer.typeSilenceDurationUs = json_fragment[
                #         "transformer"]["durationUs"]
                # elif random_type == audio_process_pipeline_pb2.Transformer.TransformerType.TYPE2:
                #     proto_fragment.transformer.type = audio_process_pipeline_pb2.Transformer.TransformerType.TYPE2
                #     proto_fragment.transformer.typeType2Param2 = random.randint(
                #         0, 100)
                #     proto_fragment.transformer.typeType2Param3 = random.randint(
                #         0, 100)
                #     proto_fragment.transformer.typeType2Param4 = random.randint(
                #         0, 100)
                # elif random_type == audio_process_pipeline_pb2.Transformer.TransformerType.TYPE3:
                #     proto_fragment.transformer.type = audio_process_pipeline_pb2.Transformer.TransformerType.TYPE3
                #     proto_fragment.transformer.typeType3Param5 = random.randint(
                #         0, 100)
                #     proto_fragment.transformer.typeType3Param6 = random.randint(
                #         0, 100)
                    ######################

                proto_fragments_collection.fragments.append(proto_fragment)

            yield audio_filepath, proto_fragments_collection.SerializeToString()
